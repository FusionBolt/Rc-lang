require './lib/helper'

def args_to_hash(*args)
  args.reduce({}) do |sum, arg|
    sum.merge(
      if arg.is_a? Hash
        arg
      else
        { arg => :str }
      end)
  end
end

class Module
  def attr_type(*args)
    args = args_to_hash(*args)
    args.map do |attr, type|
      define_method "#{attr}_t" do
        type
      end
    end
  end
end

class TypeStruct
  def self.new(*args, &block)
    # if don't have allocate, will be nil class
    obj = allocate
    # initialize is a private method
    # initialize must be send instead of direct call
    obj.send(:initialize, *args, &block)
  end

  # todo:type valid check
  def initialize(*args)
    args = args_to_hash(*args)
    Struct.new(*args.keys).tap do |klass|
      args.each do |attr, type|
        # per class Struct is different
        klass.define_method "#{attr}_t" do
          type
        end
      end
    end
  end
end

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

    class FunLabel < Label
    end

    class Addr < TypeStruct.new(:seg, :offset)
    end

    class UnsetAddr < Struct.new(:unset_addr)
    end

    class LocalVarOperator < Struct.new(:offset)
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

    class DirectJump < Struct.new(:target)
      def to_s
        "DirectJump #{target}"
      end
    end

    class Push < Struct.new(:push)
      def to_s
        "Push #{value}"
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
