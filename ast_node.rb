require './log'

# TODO:valid check
# TODO:Err Check
# TODO:call args check
class RootNode
  def initialize(packages, other)
    $logger.info '----------------Construct Ast----------------'
    @packages, @other = packages, other
    $logger.debug 'root node'
  end

  def eval(env = {})
    $logger.info '---------------- start eval ----------------'
    # import packages
    @packages.each { |p| p.eval(env) }
    # read statement
    @other.each do |s|
      case s
      in FunNode[fun_name, _, _]
        # TODO:first check valid, then add to env
        eval_log "fun:#{fun_name} has be added to env"
        env[fun_name] = s
      in ClassNode[class_name, _]
        eval_log "class:#{class_name} has be added to env"
        env[class_name] = s
      else
        # statement
        s.eval(env)
      end
    end

    if env.has_key? 'main'
      FunCallNode.new('main').eval(env)
    else
      raise 'NoMainException'
    end
  end

  def inspect(indent = nil)
    (@packages.map{|p| p.inspect} +
      @other.map{|s| s.inspect}).join("\n")
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
    env.merge import
  end

  def inspect(indent = nil)
    "import \"#{@package_name}\""
  end
end

class FunNode
  attr_reader :args
  def initialize(name, args, stmts)
    @name, @args, @stmts = name, args, stmts
    $logger.debug "implement #{inspect}"
  end

  def inspect(indent = nil)
    "fun:#{@name} args:(#{@args.map(&:inspect).join(',')})"
  end

  def deconstruct
    [@name, @args, @stmts]
  end

  def eval(env = {}, args = [])
    # TODO:merge args
    args.zip(@args)
    @stmts.eval(env)
  end
end

class ClassNode
  def initialize(name, define)
    @name, @define = name, define
    $logger.debug "implement class:#{@name}"
  end

  def inspect(indent = nil)
    "class:#{@name}"
  end

  def deconstruct
    [@name, @define]
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
    expr = @term_list.map do |term|
      term.eval(env)
    end.join(' ')
    Kernel.eval(expr)
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

  # TODO:env merge
  def eval(env = {})
    fun = env[@name]
    if fun.class == FunNode
      # fun_args => call_args
      # merge env and args
      # TODO:env merge args
      eval_log "call:#{@name}"
      fun.eval(env.dup, @args)
    else
      # TODO:exception class
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

  # TODO:if elements nil
  def inspect(indent = nil)
    @stmts.map(&:inspect)
  end

  def eval(env = {})
    @stmts.each {|s| s.eval(env)}
  end
end