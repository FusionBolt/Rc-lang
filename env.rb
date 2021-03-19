require './Lib/log'
require './Lib/error'

module Rc
  class Env
    attr_accessor :outer, :env

    def initialize(env = {}, outer = nil)
      set_self(env, outer)
    end

    # TODO:refactor inspect, more clear
    def inspect
      if @outer.nil?
        "env:#{@env}, outer:nil"
      else
        "env:#{@env}, outer:#{@outer.inspect}"
      end
    end

    def [](symbol)
      find_symbol(symbol)
    end

    def +(other)
      Env.new(@env.merge(other.env))
    end

    def merge(other)
      @env.update(other.env)
    end

    def system_var_init
      @env.update init_debug_info.merge(init_exception).merge(init_args([]))
    end

    def sub_scope(args_env = {}, &call_block)
      start_subroutine(args_env)
      return_val = call_block.call
      end_subroutine
      return_val
    end

    # TODO:when replace with other text
    # will changed every one which used it
    # changed it by other way
    def init_debug_info
      { '!!SaveMainEnv' => true }
    end

    def init_exception
      { '!!ExceptionInfo' => [] }
    end

    def init_args(argv = [])
      { '!!ARGC' => argv.length, '!!ARGV' => argv }
    end

    def define_symbol(sym, define = nil)
      if @env.has_key? sym
        raise SymbolReDefineError.new(sym)
      else
        @env[sym] = define
      end
    end

    def find_symbol(sym)
      if @env.has_key? sym
        @env[sym]
      elsif not @outer.nil?
        @outer.find_symbol(sym)
      else
        raise SymbolNotFoundError.new(sym)
      end
    end

    def update_symbol(sym, define)
      if @env.has_key? sym
        self[sym] = define
      elsif not @outer.nil?
        @outer.update_symbol(sym)
      else
        raise SymbolNotFoundError.new(sym)
      end
    end

    private

    def method_missing(sym, *args)
      hash_method = @env.method(sym)
      if hash_method.nil?
        super
      else
        hash_method.call(*args)
      end
    end

    def start_subroutine(args_env = {})
      @outer = Env.new(@env, @outer)
      @env = args_env
    end

    def end_subroutine
      if @outer.nil?
        raise "OuterEnvNil current env#{@env}"
      else
        set_self(@outer.env, @outer.outer)
      end
    end

    def set_self(env, outer)
      @env, @outer = env, outer
    end
  end
end