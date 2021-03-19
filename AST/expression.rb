require_relative 'helper'

module Rc
  class Expression
    attr_accessor :term_list

    # Distinguish between normal expr and lambda
    def initialize(expr)
      # lambda
      if expr.class == Array
        @term_list = expr
      else
        @term_list = [expr]
      end
    end

    def inspect
      if @term_list.class == Function
        @term_list.inspect
      else
        @term_list.map(&:inspect).join(' ')
      end

    end
  end

  class FunCall
    attr_reader :name, :args

    def initialize(name, args = [])
      @name, @args = name, args
      $logger.debug "call:#{inspect}"
    end

    def inspect(indent = nil)
      "#{@name}#{args_inspect(@args)}"
    end
  end

  class ClassMemberAccess
    attr_reader :instance_name, :member_name, :args

    def initialize(instance_name, member_name, args = [])
      @instance_name, @member_name, @args = instance_name, member_name, args
    end

    def inspect
      "#{@instance_name}.#{@member_name} #{@args.empty?? '' : args_inspect(@args)}"
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

    def inspect
      "#{@class_name}.new#{args_inspect(@args)}"
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

    def fetch_var(var)
      @instance_env[var]
    end

    def fetch_fun(fun)
      @class_define.fetch_member(fun)
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