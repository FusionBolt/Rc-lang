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

module TypeCheck
  VALID_TYPE = [:int, :str]
  def invalid?(type)
    VALID_TYPE.include? type
  end
end

class TypeStruct
  include TypeCheck
  def self.new(*args, &block)
    # if don't have allocate, will be nil class
    obj = allocate
    # initialize is a private method
    # initialize must be send instead of direct call
    obj.send(:initialize, *args, &block)
  end

  # todo:type valid check test
  def initialize(*args)
    args = args_to_hash(*args)
    Struct.new(*args.keys).tap do |klass|
      args.each do |attr, type|
        unless invalid? type
          raise "invalid type #{type}, only supported #{VALID_TYPE.map(&:to_s).join(',')}"
        end
        # per class Struct is different
        klass.define_method "#{attr}_t" do
          type
        end
      end
    end
  end
end