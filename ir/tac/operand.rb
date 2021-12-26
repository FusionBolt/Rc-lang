module Rc::TAC
  class Operand
  end

  class Memory < Operand
    attr_reader :addr

    def initialize(addr)
      @addr = addr
    end

    def ==(other)
      @addr == other.addr
    end
  end

  class Name < Operand
    attr_accessor :name

    def initialize(name)
      @name = name
    end

    def to_s
      @name.gsub(/:/, '')
    end

    def ==(other)
      @name == other.name
    end
  end

  class TempName < Name
  end

  class Number < Operand
    attr_accessor :num

    def initialize(num)
      @num = num
    end

    def to_s
      @num.to_s
    end

    def ==(other)
      @num == other.num
    end
  end
end
