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

    class Label < Struct.new(:name)
      def to_s
        "Label #{@name}"
      end
    end

    class FunLabel < Label
    end

    class Addr < Struct.new(:seg, :offset)
    end

    class UnsetAddr < Struct.new(:unset_addr)
    end

    class LocalVarOperator < Struct.new(:offset)
    end

    class SetLocal < LocalVarOperator
    end

    class GetLocal < LocalVarOperator
    end

    class CondJump < Struct.new(:cond, :addr)
      def to_s
        "CondJump #{cond} #{addr}"
      end
    end

    class DirectJump < Struct.new(:target)
      def to_s
        "DirectJump #{@target}"
      end
    end

    class Push < Struct.new(:push)
      def to_s
        "Push #{@value}"
      end
    end

    class Pop < Struct.new(:pos)
      def to_s
        "Pop #{pos}"
      end
    end

    class Call < Struct.new(:target)
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
