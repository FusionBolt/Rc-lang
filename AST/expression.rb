module Rc
  class Expression
    attr_accessor :term_list

    def initialize(list)
      @term_list = list
    end

    def inspect
      @term_list.map(&:inspect).join(' ')
    end
  end

  class FunCall
    attr_reader :name, :args

    def initialize(name, args = [])
      @name, @args = name, args
      $logger.debug "call:#{@name} args:(#{inspect})"
    end

    def inspect(indent = nil)
      "#{@name}(#{@args.map(&:inspect).join(',')})"
    end
  end

  class ClassMemberAccess
    attr_reader :instance_name, :member_name, :args

    def initialize(instance_name, member_name, args = [])
      @instance_name, @member_name, @args = instance_name, member_name, args
    end
  end

  class Identifier
    attr_reader :name

    def initialize(name)
      @name = name
    end

    def inspect(indent = nil)
      @name
    end
  end

  class NewExpr
    attr_reader :class_name, :args

    def initialize(class_name, args = [])
      @class_name, @args = class_name, args
    end
  end

  class Instance
    # TODO:调用成员函数传入自身去执行
    attr_reader :class_define, :instance_env, :is_obj

    def initialize(class_define, member_env, is_obj = false)
      @class_define, @instance_env = class_define, member_env
      @is_obj = is_obj
    end

    def inspect
      if @is_obj
        "#{@class_define.inspect} instance"
      else
        instance_env[:_val].inspect
      end
    end

    def call_fun(name)
      @class_define.fun_env[name].eval
    end

    def mem_var(name)
      @instance_env[name].eval
    end

    def update(member_name, val)
      @instance_env[member_name] = val
    end
  end

  class Op
    attr_accessor :op

    def initialize(op)
      @op = op
    end

    def inspect(indent = nil)
      @op
    end
  end

  class BoolConstant
    attr_reader :val

    def initialize(constant_val)
      @val = constant_val
    end

    def inspect(indent = nil)
      @val
    end
  end

  class NumberConstant
    attr_reader :val

    def initialize(constant_val)
      @val = constant_val
    end

    def inspect(indent = nil)
      @val
    end
  end

  class StringConstant
    attr_reader :val

    def initialize(constant_val)
      @val = constant_val
    end

    def inspect(indent = nil)
      @val
    end
  end
end