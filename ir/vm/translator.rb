require './ir/ast/visitor'
require './lib/visitor'
require_relative './inst'

module Rc::VM
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

  module ExprTranslator
    include Rc::AST::ExprVisitor

    def on_binary(node)
      [
        Push.new(visit(node.lhs)),
        Push.new(visit(node.rhs)),
        translate_op(node.op),
      ]
    end

    def on_bool_constant(node)
    end

    def on_number_constant(node)
      node.val.to_i
    end

    def on_string_constant(node)
    end

    def on_identifier(node)
      cur_fun_env[node.name].id
    end
  end

  module StmtTranslator
    include Rc::AST::StmtVisitor

    def on_function(node)
      @cur_fun = node.name
      super(node)
    end

    def on_assign(node)
      res = visit(node.var_obj)
      [visit(node.expr), SetLocal.new(res)]
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
