require_relative 'evaluate'
require_relative 'helper'
require_relative 'call_stack'
# TODO:传递参数
# 对象的问题
# CallStack and track the statement
# about class and instance

module Rc
  class Visitor
    def initialize(env = Env.new)
      @env = env
      @env.system_var_init
      @evaluator = Evaluate.new(self, @env)
      @call_stack = CallStack.new
    end

    def visit(node)
      method("on_#{Helper::under_score_class_name(node)}")[node]
    end

    def main(argv = [])
      if @env.has_key? 'main'
        $logger.info '---------------- start main ----------------'
        # TODO:change
        visit(Expression.new([FunCall.new('main')]))
      else
        raise 'MainNotFound'
      end
      p @env
    end

    # TODO:refactor by decorator
    def run_fun(fun, args)
      if args.length != fun.args.length
        raise 'ArgsLengthNotMatch'
      end
      args_env = fun.args.zip(args.map { |arg| @evaluator.evaluate(arg) }).to_h
      @env.subroutine(args_env) do
        @call_stack.subroutine(StackFrame.new(fun, @env)) do
          visit(fun.stmts)
        end
      end
    end

    def on_nil_class(node)
      raise 'OnNilClass'
    end

    def on_root(node)
      # TODO:import
      node.packages.each { |n| visit(n) }
      node.other.each { |n| visit(n) }
    end

    def on_package(node)
      node.name
    end

    # TODO:preprocessing?
    def on_function(node)
      @env.define_symbol(node.name, node)
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
      @env.define_symbol(node.name, node)
    end

    def on_expression(node)
      @evaluator.evaluate(node)
    end

    # TODO:need a statement visitor?
    def on_stmts(node)
      node.stmts.each do |n|
        return_val = visit(n)
        # TODO:refactor
        return return_val if n.stmt.class == Return or n == node.stmts[-1]
      end
    end

    def on_stmt(node)
      return if node.stmt == []
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
        if node.else_stmts.any?
          visit(node.else_stmts)
        end
      end
      # TODO:test
    end

    def on_assign(node)
      # TODO:成员变量赋值怎么办
      # TODO:检查var是否存在
      # TODO:未完全实现
      # TODO:成员变量赋值出错
      # TODO:env.update value
      @env[node.var_obj.name] = @evaluator.evaluate(node.expr)
    end

    def on_return(node)
      @evaluator.evaluate(node.expr)
    end

    def on_debug_stmt(node)
      p node.info
    end

    def on_break_point(node)
      # TODO:stop and get input
      # TODO:need a repl that can get val from env
      # TODO:a GUI for displaying debugging information
      # REPL
    end
  end
end