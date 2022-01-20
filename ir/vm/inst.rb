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

    class Label < Struct.new(:name)
      attr_type :name => :str
      def to_s
        "Label #{name}"
      end
    end

    class FunLabel < Struct.new(:name)
      attr_type :name => :str
      def to_s
        "FunLabel #{name}"
      end
    end

    class Addr < TypeStruct.new(:seg, :offset)
      attr_type :offset => :int
    end

    class UnsetAddr < TypeStruct.new(:unset_addr)
    end

    class LocalVarOperator < Struct.new(:offset)
      attr_type :offset => :int

      def to_s
        "#{self.demodulize_class} #{offset}"
      end
    end

    class SetLocal < Struct.new(:offset)
      attr_type :offset => :int

      def to_s
        "#{self.demodulize_class} #{offset}"
      end
    end

    class GetLocal < Struct.new(:offset)
      attr_type :offset => :int

      def to_s
        "#{self.demodulize_class} #{offset}"
      end
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

    class Push < Struct.new(:value)
      attr_type :value => :int
      def to_s
        "Push #{value}"
      end
    end

    class Pop < TypeStruct.new(:pos => :int)
      def to_s
        "Pop #{pos}"
      end
    end

    class Call < Struct.new(:target)
      attr_type :target => :str

      def to_s
        "Call #{target}"
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

    class Alloc < Struct.new(:type)
      attr_type :type => :str
    end
  end
end
