require './lib/helper'
require './lib/visitor'

module Rc::AST
  module StmtVisitor
    def on_root(node)
      node.packages.each { |n| visit(n) }
      node.defines.map { |n| visit(n) }
    end

    def on_package(node) end

    def on_function(node)
      visit(node.stmts)
    end

    def on_class_define(node) end

    def on_stmts(node)
      node.stmts.map do |n|
        visit(n)
      end
    end

    def on_stmt(node) = visit(node.stmt)

    def on_variant(node) end

    def on_if(node) end

    def on_unless(node) end

    def on_assign(node)
      visit(node.var_obj)
      visit(node.expr)
    end

    def on_return(node) end

    def on_break_point(node) end

    def on_debug_stmt(node) end
  end

  module ExprVisitor
    def on_expr(node) = visit(node.expr)

    def on_binary(node) end

    def on_lambda(node) end

    def on_fun_call(node) end

    def on_class_member_access(access) end

    def on_identifier(node) end

    def on_instance(node) end

    def on_new_expr(node) end

    # remove
    def on_op(node) end

    def on_constant(node)
      raise "node should not be a general constant #{node}"
    end

    def on_bool_constant(node) end

    def on_number_constant(node) end

    def on_string_constant(node) end

    def on_invoke_super(node) end
  end

  module Visitor
    include StmtVisitor
    include ExprVisitor
    include Rc::Lib::Visitor
  end
end