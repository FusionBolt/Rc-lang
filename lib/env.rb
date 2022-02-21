require './lib/log'
require './lib/error'
require './lib/hack'

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
      if other.is_a? Env
        @env.update(other.env)
      else
        @env.update(other)
      end
    end

    def system_var_init
      @env.update init_debug_info.merge(init_exception).merge(init_args([]))
    end

    def sub_scope(args_env = {}, &call_block)
      start_subroutine(args_env)
      call_block.try(&:call).tap { end_subroutine }
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
        define
      end
    end

    def has_symbol?(sym)
      if @env.has_key? sym
        true
      elsif not @outer.nil?
        @outer.has_symbol?(sym)
      else
        false
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

    # todo:fix this, nest env maybe error
    def map(&block)
      @env.map(&block)
    end
    private

    def method_missing(sym, *args, &block)
      @env.method(sym).try {|x| x.call(*args, &block)}
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

    def empty?
      if @outer.nil?
        @env.empty?
      else
        @env.empty? && @outer.empty?
      end
    end
  end

  class ConstTable
    attr_reader :list

    def initialize
      @list = []
    end

    def add(constant)
      # todo:slow, if @list == Set[], maybe can't index
      i = @list.index(constant)
      i.or_else do
        @list.push constant
        @list.size - 1
      end
    end

    private def method_missing(symbol, *args)
      @list.method(symbol).try { |x| x.call(*args) }
    end

    def ==(other)
      if other.is_a? Array
        @list == other
      else
        @list.== other.list
      end
    end
  end

  # used for vm
  class FunTable
    attr_accessor :local_sym_table, :args, :offset

    def initialize(local_sym_table, args, offset)
      @local_sym_table, @args, @offset = local_sym_table, args, offset
    end
  end

  # todo:this maybe create by define symbol
  class EnvItemInfo < Struct.new(:id, :type)
  end

  class InstanceMethodInfo < Struct.new(:define, :env, :args)
    def argc
      args.size
    end
  end

  class ClassTable
    attr_accessor :instance_methods, :instance_vars, :parent

    def initialize
      @instance_methods = {}
      @instance_vars = {}
      @parent = ""
    end

    def add_instance_method(name, define)
      @instance_methods[name] = define
    end

    def add_instance_var(name)
      @instance_vars[name] = @instance_vars.size
    end

    def instance_var_keys
      @instance_vars.sort_by(&:last).map {|k, _|k}
    end

    # todo:add test
    def add_parents(parent_name, parent_table)
      @parent = parent_name
      unless parent_table.is_a? ClassTable
        raise "parent_table should be a ClassTable"
      end
      parent_table.instance_vars.each do |var_name, _|
        unless @instance_vars.include? var_name
          add_instance_var(var_name)
        end
      end
    end
  end

  class GlobalEnv < Struct.new(:const_table, :class_table)
  end
end