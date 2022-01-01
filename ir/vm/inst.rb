require './lib/helper'

module Rc::VM
  module Inst
    # to_s util method
    module InstUtil
      def to_s
        self.demodulize_class
      end

      def ==(other)
        self.class == other.class
      end
    end

    class Label
      attr_reader :name

      def initialize(name)
        @name = name
      end

      def to_s
        "Label #{@name}"
      end
    end

    class FunLabel < Label
    end

    class Addr
      attr_reader :seg, :offset

      def initialize(seg, offset)
        @seg, @offset = seg, offset
      end
    end

    class UnsetAddr
      attr_reader :name

      def initialize(name)
        @name = name
      end
    end

    class LocalVarOperator
      attr_accessor :offset

      def initialize(offset)
        @offset = offset
      end

      def ==(other)
        offset == other.offset
      end
    end

    class SetLocal < LocalVarOperator
    end

    class GetLocal < LocalVarOperator
    end

    class CondJump
      attr_accessor :cond, :addr

      def initialize(cond, addr)
        @cond, @addr = cond, addr
      end

      def to_s
        "CondJump #{cond} #{addr}"
      end
    end

    class DirectJump
      attr_reader :target

      def initialize(target)
        @target = target
      end

      def to_s
        "DirectJump #{@target}"
      end
    end

    class Push
      attr_reader :value

      def initialize(value)
        @value = value
      end

      def to_s
        "Push #{@value}"
      end

      def ==(other)
        @value == other.value
      end
    end

    class Pop
      attr_reader :pos

      def initialize(pos)
        @pos = pos
      end

      def to_s
        "Pop #{pos}"
      end
    end

    class Call
      attr_reader :target

      def initialize(target)
        @target = target
      end

      def ==(other)
        @target == other.target
      end
    end

    class Return
      include InstUtil
    end

    class Add
      include InstUtil
    end

    class Sub
      include InstUtil
    end

    class Mul
      include InstUtil
    end

    class Div
      include InstUtil
    end
  end
end
