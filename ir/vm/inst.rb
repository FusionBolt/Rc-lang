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

    class PushThis
      include InstUtil
    end

    class Pop < TypeStruct.new(:pos => :int)
      def to_s
        "Pop #{pos}"
      end
    end

    class Call < Struct.new(:target, :argc)
      attr_type :target => :str
      attr_type :argc => :int

      def to_s
        "Call #{target} #{argc}"
      end
    end

    class InvokeSuper < Struct.new(:argc)
      attr_type :argc => :int

      def to_s
        "InvokeSuper #{argc}"
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

    class Alloc < Struct.new(:class_type)
      attr_type :class_type => :str

      def to_s
        "Alloc #{class_type}"
      end
    end

    class GetClassMemberVar < Struct.new(:id)
      attr_type :id => :int
      def to_s
        "GetClassMemberVar #{id}"
      end
    end

    class SetClassMemberVar < Struct.new(:id)
      attr_type :id => :int
      def to_s
        "SetClassMemberVar #{id}"
      end
    end
  end
end
