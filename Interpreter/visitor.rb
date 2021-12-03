require_relative 'evaluator'
require_relative '../helper'
require_relative 'call_stack'
# TODO:传递参数
# 对象的问题
# CallStack and track the statement
# about class and instance

module Rc
  module Interpret
    class Visitor
      def initialize(env = Env.new)
        @env = env
        @env.system_var_init
        @call_stack = CallStack.new
        @evaluator = Evaluator.new(self, @call_stack, @env)
      end

      def cur_stmt
        @call_stack.cur_stmt
      end

      def set_cur_stmt(stmt)
        @call_stack.cur_stmt = stmt
        $logger.info("#{@call_stack.indent}#{stmt.inspect}")
      end

      def visit(node)
        return if @call_stack.error
        begin
          method("on_#{Helper::under_score_class_name(node)}")[node]
        rescue Rc::RuntimeError, Rc::SemanticError => e
          @call_stack.raise(e, @env)
        end
      end

      def main(argv = [])
        if @env.has_key? 'main'
          $logger.info '---------------- start main ----------------'
          visit(Stmt.new(Expr.new(FunCall.new('main', argv))))
        else
          @call_stack.raise(Rc::RuntimeError.new, @env)
        end
      end

      def on_root(node)
        # TODO:import
        node.packages.each { |n| visit(n) }
        node.defines.map { |n| visit(n) }
      end

      def on_package(node)
        node.name
      end

      # TODO:preprocessing?
      def on_function(node)
        @env.define_symbol(node.name, node)
        node
      end

      def on_class_define(node)
        init_fun = node.init
        if init_fun.nil?
          node.generate_init_fun
        else
          # some var may not be initialized state
          # look up which var not be initialized
          # TODO:not implement
          # init_fun.stmts.append()
        end
        node.get_parent(@env)
        @env.define_symbol(node.name, node)
      end

      def on_expr(node)
        @evaluator.evaluate(node)
      end

      def on_stmts(node)
        node.stmts.each do |n|
          return_val = visit(n)
          # TODO:refactor
          return return_val if n.stmt.class == Return or n == node.stmts[-1]
        end
      end

      def on_stmt(node)
        return if node.stmt == []
        set_cur_stmt(node)
        visit(node.stmt)
      end

      def on_variant(node)
        @env.define_symbol(node.name, @evaluator.evaluate(node.expr))
      end

      def on_if(node)
        if @evaluator.evaluate(node.if_cond)
          visit(node.if_stmts)
        else
          node.elsif_list.each do |e|
            if @evaluator.evaluate(e[0])
              return visit(e[1])
            end
          end
          visit(node.else_stmts)
        end
      end

      def on_unless(node)
        unless @evaluator.evaluate(node.cond)
          visit(node.stmts)
        end
      end

      def on_assign(node)
        # TODO:when impl more check, should check sym is defined
        # TODO:检查var是否存在
        # TODO:未完全实现
        # TODO:成员变量赋值出错
        # TODO:env.update value
        node_v = @evaluator.evaluate(node.expr)
        if node.var_obj.class == ClassMemberAccess
          @call_stack.cur_obj[node.var_obj.member_name] = node_v
        else
          @env[node.var_obj.name] = node_v
        end
      end

      def on_return(node)
        @evaluator.evaluate(node.expr)
      end

      def on_debug_stmt(node)
        1 + 1
      end

      def on_break_point(node)
        # TODO:stop and get input
        # TODO:need a repl that can get val from env
        # TODO:a GUI for displaying debugging information
        # REPL
      end
    end
  end
end