require './log'
# TODO:valid check
# TODO:Err Check

class RootNode
  def initialize(packages, other)
    @packages, @other = packages, other
    $logger.debug 'root node'
  end

  def eval(env = {})
    $logger.info '---------------- start eval ----------------'
    # import packages
    @packages.each { |p| p.eval(env) }
    # read statement
    @other.each do |s|
      s.eval(env)
    end
    $logger.info '---------------- end eval ----------------'
    self
  end

  def inspect(indent = nil)
    (@packages.map { |p| p.inspect } +
      @other.map { |s| s.inspect }).join("\n")
  end

  def main(env = {}, argv = [])
    if env.has_key? 'main'
      FunCallNode.new('main').eval(env)
    else
      raise 'MainNotFound'
    end
  end
end

class PackageNode
  def initialize(name)
    @name = name
    $logger.debug "import package:#{@name}"
  end

  def import
    # TODO:parser import file, get env
    # multi main err
    # if error, then save the err msg
    {}
  end

  def eval(env = {})
    env.update import
  end

  def inspect(indent = nil)
    "import \"#{@package_name}\""
  end
end

class FunNode
  # TODO:Distinguish between member fun and normal fun
  attr_reader :name, :args, :stmts

  def initialize(name, args, stmts)
    @name, @args, @stmts = name, args, stmts
    $logger.debug "implement #{inspect}"
  end

  def inspect(indent = nil)
    "fun:#{@name} args:(#{@args.map(&:inspect).join(',')})"
  end

  def eval(env = {})
    if @stmts.valid?
      eval_log "fun:#{@name} has be added to env"
      env[@name] = self
    else
      eval_log "fun:#{@name} body has error"
    end
  end

  def call(env = {}, args = [])
    if args.length != @args.length
      raise 'ArgsLengthNotMatch'
    end
    if env['!!SaveMainEnv']
      @stmts.eval(env.update(args_env(args)))
    else
      # not save env in main
      @stmts.eval(env.merge(args_env(args)))
    end
  end

  def args_env(actual_args)
    @args.zip(actual_args).to_h
  end

  def args_valid_check
    @args.length == @args.uniq.length
  end
end

class ClassNode
  def initialize(name, define, parent = nil)
    @name, @define, @parent = name, define, parent
    # TODO:should be changed
    @fun_list = @define.select { |d| d.class == FunNode }
    @var_list = @define.select { |d| d.class != FunNode }
    $logger.debug "implement class:#{@name}"
  end

  def inspect(indent = nil)
    "class:#{@name}"
  end

  def eval(env = {})
    init_fun = @fun_list.detect { |f| f.name == 'init' }
    if init_fun.nil?
      # no init
      @fun_list << generate_init_fun
    else
      # some var may not be initialized state
      # look up which var not be initialized
      # TODO:not implement
      # init_fun.stmts.append()
    end
    eval_log "class:#{@name} has be added to env"
    env[@name] = self
  end

  def generate_init_fun
    eval_log "class:#{@name} generate init fun"
    # TODO:finish
    # user defined var need init
    FunNode.new('init', [],
                DebugStatement.new('generate empty init'))
  end

  def instance_constructor
    fun_env['init']
  end

  def fun_env
    @fun_list.map { |f| [f.name, f] }.to_h
  end

  def var_env

  end

  def full_env
    fun_env + var_env
  end
end

class DebugStatement
  def initialize(info = '')
    @info = info
  end

  def eval(env = {})
    eval_log "debug statement#{@info}"
  end
end

class InstanceNode
  # TODO:调用成员函数传入自身去执行
  attr_reader :class_define, :instance_env, :is_obj

  def initialize(class_define, member_env, is_obj = false)
    @class_define, @instance_env = class_define, member_env
    @is_obj = is_obj
  end

  def eval(env = {})
    if @is_obj
      raise 'UnfinishedException'
    else
      instance_env[:_val].eval(env)
    end
  end

  def inspect
    if @is_obj
    else
      instance_env[:_val].inspect
    end
  end

  def access(member_name, args = [], env = {})
    # TODO:refactor
    # find in fun, if not exist, then find var
    if @class_define.fun_env.include? member_name
      @class_define.fun_env[member_name].call(env, args)
      # TODO:instance_env add instance fun define
      # and need to distinguish between var and fun
    elsif @instance_env.include? member_name
      @instance_env[member_name].eval(env)
    else
      raise 'NoMember'
    end
  end

  def call_fun(name)
    @class_define.fun_env[name].eval
  end

  def mem_var(name)
    @instance_env[name].eval
  end

  def update(member_name, val)
    @instance_env[member_name] = val
  end
end

class ClassMemberAccessNode
  def initialize(instance_name, member_name, args = [])
    @instance_name, @member_name, @args = instance_name, member_name, args
  end

  def eval(env = {})
    eval_log "class member access #{@instance_name}.#{@member_name}"
    env[@instance_name].access(@member_name)
  end

  def update(val, env)
    env[@instance_name].update(val)
  end
end

class NewExprNode
  def initialize(class_name, args = [])
    @class_name, @args = class_name, args
  end

  def eval(env = {})
    class_node = env[@class_name]
    class_node.instance_constructor.call(env, @args)
    InstanceNode.new(class_node, class_node.var_env, true)
  end
end

class StatementNode
  def initialize(stmt)
    @statement = stmt
  end

  def eval(env = {})
    return if @statement == []
    eval_log "#{@statement.inspect}"
    @statement.eval(env)
  end

  def inspect(indent = nil)
    @s.inspect
  end
end

class AssignNode
  # TODO:测试成员赋值
  def initialize(var_obj, expr)
    @var_obj, @expr = var_obj, expr
    $logger.debug "#{var_obj} assign #{expr}"
  end

  # TODO:成员变量赋值怎么办
  # TODO:检查var是否存在
  # TODO:未完全实现
  def eval(env = {})
    @var_obj.update(@expr.eval(env), env)
  end
end

class VariantNode
  def initialize(name, expr)
    @name, @expr = name, expr
    $logger.debug "var:#{@name} val:#{@expr.inspect}"
  end

  def inspect(indent = nil)
    "var #{@name} = #{@expr.inspect}"
  end

  def eval(env = {})
    env[@name] = @expr.eval(env)
  end
end

class IfNode
  def initialize(if_cond, if_stmts, elsif_node, else_node)
    @if_condition, @if_statements, @elsif_list, @else_statements =
      if_cond, if_stmts, elsif_node, elsif_node
    $logger.debug "if node"
  end

  def inspect(indent = nil)
    'if'
  end

  def eval(env = {})
    # TODO:elsif else test
    if @if_condition.eval(env)
      eval_log 'if stmts'
      @if_statements.eval(env)
    else
      # else if
      @elsif_list.each do |e|
        eval_log 'elsif stmts'
        if e[0].eval(env)
          return e[1].eval(env)
        end
      end
      # TODO:return error
      # else
      unless @else_statements == []
        eval_log 'else stmts'
        @else_statements.eval(env)
      end
    end
  end
end

class OpNode
  attr_accessor :op

  def initialize(op)
    @op = op
  end

  def inspect(indent = nil)
    @op
  end

  def eval(env = {})
    @op
  end
end

class ExprNode
  attr_accessor :term_list

  def initialize(list)
    @term_list = list
  end

  def inspect
    @term_list.map(&:inspect).join(' ')
  end

  def eval(env = {})
    # TODO:when fun call
    # TODO:distinguish between new,obj and other that can be eval
    if @term_list.length == 1
      @term_list[0].eval(env)
    else
      expr = @term_list.map do |term|
        term.eval(env)
      end.join(' ')
      Kernel.eval(expr)
    end
  end
end

class FunCallNode
  def initialize(name, args = [])
    @name, @args = name, args
    $logger.debug "call:#{@name} args:(#{inspect})"
  end

  def inspect(indent = nil)
    "#{@name}(#{@args.map(&:inspect).join(',')})"
  end

  def eval(env = {})
    fun = env[@name]
    if fun.class == FunNode
      eval_log "call:#{@name}"
      fun.call(env, @args)
    else
      raise "call #{@name} failed, error env:: #{env}"
    end
  end
end

class IdentifierNode
  def initialize(name)
    @name = name
  end

  def inspect(indent = nil)
    @name
  end

  def eval(env = {})
    # TODO:if name not exist, throw exception
    env[@name]
  end

  def update(val, env)
    env[@name] = val
  end
end

class BoolConstant
  def initialize(constant_val)
    @val = constant_val
  end

  def inspect(indent = nil)
    @val
  end

  def eval(env = {})
    Kernel.eval(@val)
  end
end

class NumberConstant
  def initialize(constant_val)
    @val = constant_val
  end

  def inspect(indent = nil)
    @val
  end

  def eval(env = {})
    Kernel.eval(@val)
  end
end

class StringConstant
  def initialize(constant_val)
    @val = constant_val
  end

  def inspect(indent = nil)
    @val
  end

  def eval(env = {})
    @val
  end
end

class BoolConstant
  def initialize(constant_val)
    @val = constant_val
  end

  def inspect(indent = nil)
    @val
  end

  def eval(env = {})
    Kernel.eval(@val)
  end
end

class StatementsNode
  attr_reader :stmts

  def initialize(stmts)
    @stmts = stmts
  end

  def inspect(indent = nil)
    @stmts.map(&:inspect)
  end

  def eval(env = {})
    @stmts.each { |s| s.eval(env) }
  end

  def append(stmt)
    @stmts << stmt
  end

  def valid?
    true
  end
end

class ReturnNode
  def initialize(expr)
    @expr = expr
  end

  def inspect
    "return #{@expr.inspect}"
  end

  def eval(env = {})
    @expr.eval(env)
  end
end

class BreakPoint
  def initialize
    p "break point init"
  end

  def eval(env = {})
    p env
  end
end