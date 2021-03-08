require './Lib/log'
require_relative 'expression'
require_relative 'statement'
# TODO:valid check
# TODO:Err Check

class Root
  attr_reader :packages, :other
  def initialize(packages, other)
    @packages, @other = packages, other
  end

  def inspect(indent = nil)
    (@packages.map { |p| p.inspect } +
      @other.map { |s| s.inspect }).join("\n")
  end
end

class Package
  attr_reader :name
  def initialize(name)
    @name = name
    $logger.debug "import package:#{@name}"
  end

  def inspect(indent = nil)
    "import \"#{@package_name}\""
  end
end

class Function
  # TODO:Distinguish between member fun and normal fun
  attr_reader :name, :args, :stmts

  def initialize(name, args, stmts)
    @name, @args, @stmts = name, args, stmts
    $logger.debug "implement #{inspect}"
  end

  def inspect(indent = nil)
    "fun:#{@name} args:(#{@args.map(&:inspect).join(',')})"
  end

  def args_env(actual_args, env)
    @args.zip(actual_args.map{|arg| arg.eval(env)}).to_h
  end

  def args_valid_check
    @args.length == @args.uniq.length
  end

  def deconstruct
    [@name, @args, @stmts]
  end
end

class ClassDefine
  attr_reader :name, :define, :parent
  def initialize(name, define, parent = nil)
    @name, @define, @parent = name, define, parent
    @fun_list = @define.select { |d| d.class == Function }
    @var_list = @define.select { |d| d.class != Function }
    $logger.debug "implement class:#{@name}"
  end

  def inspect(indent = nil)
    "class:#{@name}"
  end

  def init
    @fun_list.detect { |f| f.name == 'init' }
  end

  def generate_init_fun
    eval_log "class:#{@name} generate init fun"
    # TODO:finish
    # user defined var need init
    @fun_list << Function.new('init', [],
                 DebugStatement.new('generate empty init'))
  end

  def instance_constructor
    fun_env['init']
  end

  def fun_env
    @fun_list.map { |f| [f.name, f] }.to_h
  end

  def var_env
    @var_list.map { |v| [v.name, v.val] }.to_h
  end

  def full_env
    fun_env + var_env
  end

  def deconstruct
    [@name, @define, @parent]
  end
end

class ClassMemberVar
  attr_reader :name, :val

  def initialize(name, val = nil)
    eval_log "class member var #{@name}:#{@val}"
    @name, @val = name, val
  end
end