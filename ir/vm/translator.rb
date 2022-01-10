require './ir/ast/visitor'
require './lib/visitor'
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
    def on_bool_constant(node)
    end

    def on_number_constant(node)
      Value.new node.val.to_i
    end

    def on_string_constant(node)
    end

    # Get or Set, so need return a id
    def on_identifier(node)
      Ref.new cur_fun_env[node.name].id
    end

    def on_fun_call(fun_call)
      fun_call.args.map { |arg| push(visit(arg)) } + [Call.new(fun_call.name)]
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
      @global_env.define_env[node.name] = node.args
      [FunLabel.new(node.name), super(node), Return.new, FunEnd.new]
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
    def translate(ast, global_env)
      @global_env = global_env
      visit(ast).flatten.compact
    end

    def cur_fun_env
      @global_env.fun_env[@cur_fun]
    end
  end
end
