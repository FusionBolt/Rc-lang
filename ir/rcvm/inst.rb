module Rc
  module RCVM
    # to_s util method
    module InstUtil
      def to_s
        Helper.pure_class_name(self)
      end
    end

    class Label
      attr_reader :name

      def initialize(name)
        @name = name
      end

      def to_s
        "Label: #{@name}"
      end
    end

    class CondJump
      attr_accessor :cond, :addr

      def initialize(cond, addr)
        @cond, @addr = cond, addr
      end

      def to_s
        "Cond #{cond} #{addr}"
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
