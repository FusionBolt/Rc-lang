module Rc
  class StackFrame
    # TODO:save this obj
    # TODO:how to process env
    attr_accessor :caller_frame, :fun, :env
    def initialize(fun, env)
      @fun, @env = fun, env
      @caller_frame = nil
    end
  end

  class CallStack
    def initialize
      @stack = []
    end

    def depth
      @stack.length
    end

    # TODO: how to separate log info from call stack and visitor? learn more
    def pop
      puts "#{indent}return:#{top.fun.name}"
      @stack.pop
    end

    def push(frame)
      @stack.push(frame)
      puts "#{indent}call:#{frame.fun.name}"
    end

    def top
      @stack[-1]
    end

    def subroutine(new_frame, &block)
      ret_val = nil
      push(new_frame)
      if block_given?
        ret_val = block.call
      end
      pop
      ret_val
    end

    private
    def indent
      '  ' * depth
    end
  end
end