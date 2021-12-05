module Rc
  class Stmts
    attr_reader :stmts

    def initialize(stmts)
      @stmts = stmts
    end

    def to_s(indent = nil)
      @stmts.map(&:to_s)
    end

    def append(stmt)
      @stmts << stmt
    end

    def [](index)
      @stmts[index]
    end

    # TODO: tract same pattern
    private
    def method_missing(symbol, *args)
      arr_method = @stmts.method(symbol)
      if arr_method.nil?
        super
      else
        arr_method.call(*args)
      end
    end
  end

  class Stmt
    attr_reader :stmt

    def initialize(stmt, input = '', interval = '')
      @stmt = stmt
      @input, @interval = input, interval
    end

    def to_s(indent = nil)
      # TODO:string include escape character \n?
      stmt.to_s
      # @input[@interval].gsub("\n", '')
    end

    def is_empty?
      @stmt.class == Empty
    end
  end

  class Variant
    attr_reader :name, :expr

    def initialize(name, expr)
      @name, @expr = name, expr
      $logger.debug "var:#{@name} val:#{@expr.to_s}"
    end

    def to_s(indent = nil)
      "var #{@name} = #{@expr.to_s}"
    end
  end

  class If
    attr_reader :if_cond, :if_stmts, :elsif_list, :else_stmts

    def initialize(if_cond, if_stmts, elsif_node, else_node)
      @if_cond, @if_stmts, @elsif_list, @else_stmts =
        if_cond, if_stmts, elsif_node, else_node
      $logger.debug "if node"
    end

    def to_s(indent = nil)
      'if'
    end
  end

  class Unless
    attr_reader :cond, :stmts
    def initialize(cond, stmts)
      @cond, @stmts = cond, stmts
    end

    def to_s
      'unless'
    end
  end

  class Assign
    # because of in the left maybe a member access
    # TODO:set left val and right val concept
    attr_reader :var_obj, :expr

    def initialize(var_obj, expr)
      @var_obj, @expr = var_obj, expr
      $logger.debug "#{to_s}"
    end

    def to_s
      "#{@var_obj.to_s} = #{@expr.to_s}"
    end
  end

  class Return
    attr_reader :expr

    def initialize(expr)
      @expr = expr
      $logger.debug to_s
    end

    def to_s
      "return #{@expr.to_s}"
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