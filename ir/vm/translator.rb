require './ir/ast/visitor'
require './lib/visitor'
require './lib/env'
require_relative './inst'

module Rc::VM

  module VMInstOperand
    include Rc::VM::Inst

    class Value < Struct.new(:value)
    end

    # Ref a exist var
    class Ref < Struct.new(:ref)
    end

    def push(node)
      if node.is_a? Value
        Push.new(node.value)
      elsif node.is_a? Ref
        GetLocal.new(node.ref)
      else
        raise "Unsupported node type #{node.class}"
      end
    end

    def push_args(args)
      args.map { |arg| push(visit(arg)) }
    end
  end

  module ExprTranslator
    include Rc::AST::ExprVisitor
    include VMInstOperand
    include Inst

    def on_binary(node)
      [
        push(visit(node.lhs)),
        push(visit(node.rhs)),
        translate_op(node.op),
      ]
    end

    def translate_op(op)
      case op.op
      in '+'
        Add.new
      in '-'
        Sub.new
      in '*'
        Mul.new
      in '/'
        Div.new
      else
        raise 'unsupported op'
      end
    end

    # value, directly push
    def on_bool_constant(node) end

    def on_number_constant(node)
      Value.new node.val.to_i
    end

    def on_string_constant(node) end

    # Get or Set, so need return a id
    def on_identifier(node)
      Ref.new cur_fun_env[node.name].id
    end

    def on_fun_call(fun_call)
      [PushThis.new] + push_args(fun_call.args) + [Call.new(fun_call.name, fun_call.args.size)]
    end

    def on_new_expr(new)
      [Alloc.new(new.class_name), Call.new(Rc::Define::ConstructorMethod, 0)]
    end

    # todo:can't process static fun
    def on_class_member_access(access)
      argc = access.args.size
      push_this = if access.instance_name == "self"
        PushThis.new
      else
        push Ref.new cur_fun_env[access.instance_name].id
      end
      # todo: it's error when member is var
      call = Call.new(access.member_name, argc)
      [push_this] + push_args(access.args) + [call]
    end

    def on_invoke_super(node)
      [PushThis.new] + push_args(node.args.map) + [InvokeSuper.new(node.args.size)]
    end
  end

  module StmtTranslator
    include Rc::AST::StmtVisitor
    include VMInstOperand
    include Rc::VM::Inst

    def on_root(node)
      node.defines.map { |n| visit(n) }
    end

    def on_function(node)
      @cur_fun = node.name
      [FunLabel.new(node.name), super(node), Return.new]
    end

    def on_assign(node)
      res = visit(node.var_obj)
      [visit(node.expr), SetLocal.new(res.ref)]
    end
  end

  class VMInstTranslator
    include ExprTranslator
    include StmtTranslator
    include Rc::Lib::Visitor

    def translate(global_env)
      # todo:refactor
      @global_env = global_env
      global_env.class_table.update_values do |class_name, table|
        @cur_class_name = class_name
        table.instance_methods.update_values do |f_name, method_info|
          @cur_method_info = method_info
          method_info.define = visit(method_info.define).flatten.compact
          method_info
        end
        table
      end
      global_env
    end

    def cur_fun_env
      @cur_method_info.env
    end

    def cur_class_table
      @global_env.class_table[@cur_class_name]
    end
  end
end
