require './Lib/hack'

module Rc
  class RcError < StandardError
    attr_reader :error_stmt, :error_info

    def initialize(error_stmt, error_info)
      @error_stmt = error_stmt
      @error_info = error_info
    end

    def error_stmt
      @error_stmt.inspect
    end
  end

  class StackFrame
    # TODO:how to process env
    attr_reader :fun, :env, :cur_stmt, :cur_obj
    def initialize(cur_stmt, fun, env, cur_obj = nil)
      @cur_stmt, @cur_obj = cur_stmt, cur_obj
      @fun, @env = fun, env
    end
  end

  class CallStack
    # TODO:begin is main frame, remove cur_stmt
    attr_accessor :cur_stmt
    attr_reader :error
    # TODO:refactor
    def initialize
      @stack = []
      @error = false
      # stmt is class Stmt, this is used for regular print ExceptionStack
      @cur_stmt = :main
    end

    def depth
      @stack.length
    end

    def cur_obj
      @stack[-1].cur_obj
    end

    # TODO: how to separate log info from call stack and visitor? learn more
    def pop
      $logger.debug "#{indent}return:#{top.fun.name}"
      @stack.pop
    end

    def push(frame)
      @stack.push(frame)
      $logger.debug "#{indent}call:#{frame.fun.name}"
    end

    def top
      @stack[-1]
    end

    def subroutine(new_frame, &block)
      push(new_frame)
      block.try(&:call).tap { pop }
    end

    # TODO:output link to the source
    def raise(rc_error, env = Env.new)
      @error = true
      puts "\033[31mError Type:#{rc_error.class}\nError Info:#{rc_error.inspect}\nin:#{cur_stmt.inspect}\n#{error_call_stack}\n#{env.inspect}"
      exit
    end

    def indent
      '  ' * depth
    end

    private
    def error_call_stack
      @stack.reverse.map{|stack| "in:#{stack.cur_stmt.inspect}"}.join("\n")
    end
  end
end