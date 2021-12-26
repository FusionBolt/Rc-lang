module Rc::TAC
  class Quad
    attr_accessor :op, :result, :lhs, :rhs

    def initialize(op, result, lhs, rhs)
      @op, @result, @lhs, @rhs = op, result, lhs, rhs
    end

    def to_s
      "#{@result} = #{@lhs} #{@op} #{@rhs}"
    end

    def ==(other)
      @op == other.op && @result == other.result && @lhs == other.lhs && @rhs == other.rhs
    end

    def to_operand
      @result
    end
  end

  class Assign < Quad
    def initialize(result, lhs)
      @op = 'assign'
      @result = result
      @lhs = lhs
      @rhs = EmptyValue.new
    end
  end

  class Call < Quad
    def initialize(result, target, args)
      @op = 'call'
      @result = result
      @lhs = target
      @rhs = args
    end

    def target
      @lhs
    end

    def args
      @rhs
    end
  end
end
