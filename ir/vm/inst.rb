require './lib/helper'
require './lib/type_struct'

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

    class Label < TypeStruct.new(:name)
      def to_s
        "Label #{name}"
      end
    end

    class FunLabel < Label
    end

    class Addr < TypeStruct.new(:seg, :offset)
      attr_type :offset => :int
    end

    class UnsetAddr < TypeStruct.new(:unset_addr)
    end

    class LocalVarOperator < TypeStruct.new(:offset)
    end

    class SetLocal < LocalVarOperator
    end

    class GetLocal < LocalVarOperator
    end

    class CondJump < TypeStruct.new(:cond, :addr => :int)
      def to_s
        "CondJump #{cond} #{addr}"
      end
    end

    class DirectJump < TypeStruct.new(:target)
      def to_s
        "DirectJump #{target}"
      end
    end

    class Push < TypeStruct.new(:push => :int)
      def to_s
        "Push #{value}"
      end
    end

    class Pop < TypeStruct.new(:pos => :int)
      def to_s
        "Pop #{pos}"
      end
    end

    class Call < TypeStruct.new(:target)
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
