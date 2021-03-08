# TODO:method missing and find array
# TODO:behavior like array
class Statements
  attr_reader :stmts

  def initialize(stmts)
    @stmts = stmts
  end

  def inspect(indent = nil)
    @stmts.map(&:inspect)
  end

  def append(stmt)
    @stmts << stmt
  end

  def [](index)
    @stmts[index]
  end
end

class Statement
  attr_reader :statement

  def initialize(stmt)
    @statement = stmt
  end

  def inspect(indent = nil)
    @statement.inspect
  end
end

class Variant
  attr_reader :name, :expr

  def initialize(name, expr)
    @name, @expr = name, expr
    $logger.debug "var:#{@name} val:#{@expr.inspect}"
  end

  def inspect(indent = nil)
    "var #{@name} = #{@expr.inspect}"
  end
end

class If
  attr_reader :if_condition, :if_statements, :elsif_list, :else_statements
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

class Assign
  attr_reader :var_obj, :expr
  def initialize(var_obj, expr)
    @var_obj, @expr = var_obj, expr
    $logger.debug "#{var_obj} assign #{expr}"
  end
end

class Return
  attr_reader :expr
  def initialize(expr)
    @expr = expr
  end

  def inspect
    "return #{@expr.inspect}"
  end
end

class DebugStatement
  attr_reader :info
  def initialize(info = '')
    @info = info
  end
end

class BreakPoint
end
