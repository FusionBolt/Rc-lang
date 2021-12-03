module Rc
  module Visitor
    def visit(node)
      method("on_#{Helper::under_score_class_name(node)}")[node]
    end

    def on_root(node)
      node.packages.each { |n| visit(n) }
      node.defines.map { |n| visit(n) }
    end

    def on_package(node) end

    def on_function(node) end

    def on_class_define(node) end

    def on_expr(node) end

    def on_stmts(node)
      node.stmts.each do |n|
        visit(n)
      end
    end

    def on_stmt(node) end

    def on_variant(node) end

    def on_if(node) end

    def on_unless(node) end

    def on_assign(node) end

    def on_return(node) end

    def on_break_point(node) end

    def on_debug_stmt(node) end

    def on_expr(node) end

    def on_lambda(node) end

    def on_fun_call(node) end

    def on_class_member_access(access) end

    def on_identifier(node) end

    def on_instance(node) end

    def on_new_expr(node) end

    def on_op(node) end

    def on_constant(node) end

    def on_bool_constant(node) end

    def on_number_constant(node) end

    def on_string_constant(node) end
  end
end