# TODO:method missing and find array
# TODO:behavior like array
module Rc
  class Stmts
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

  class Stmt
    attr_reader :stmt

    def initialize(stmt, input, interval)
      @stmt = stmt
      @input, @interval = input, interval
    end

    def inspect(indent = nil)
      # TODO:string include escape character \n?
      @input[@interval].gsub("\n", '')
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
    attr_reader :if_cond, :if_stmts, :elsif_list, :else_stmts

    def initialize(if_cond, if_stmts, elsif_node, else_node)
      @if_cond, @if_stmts, @elsif_list, @else_stmts =
        if_cond, if_stmts, elsif_node, else_node
      $logger.debug "if node"
    end

    def inspect(indent = nil)
      'if'
    end
  end

  class Assign
    attr_reader :var_obj, :expr

    def initialize(var_obj, expr)
      @var_obj, @expr = var_obj, expr
      $logger.debug "#{inspect}"
    end

    def inspect
      "#{@var_obj.inspect} = #{@expr.inspect}"
    end
  end

  class Return
    attr_reader :expr

    def initialize(expr)
      @expr = expr
      $logger.debug inspect
    end

    def inspect
      "return #{@expr.inspect}"
    end
  end

  class DebugStmt
    attr_reader :info

    def initialize(info = '')
      @info = info
    end
  end

  class BreakPoint
  end
end