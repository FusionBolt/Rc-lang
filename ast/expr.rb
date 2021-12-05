require_relative 'helper'

module Rc
  class Expr
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

    def to_s
      if @term_list.class == Function
        @term_list.to_s
      else
        @term_list.map(&:to_s).join(' ')
      end
    end

    def is_constant?
      @term_list.all? do |term|
        if term.is_a? Expr
          if term.is_constant?
            true
          else
            return false
          end
        elsif term.is_a? Constant or term.is_a? Op
          true
        else
          return false
        end
      end
    end
  end

  class Binary
    attr_reader :op, :lhs, :rhs

    def initialize(op, lhs, rhs)
      @op, @lhs, @rhs = op, lhs, rhs
    end

    def to_s
      "#{lhs} #{op} #{rhs}"
    end
  end

  class FunCall
    attr_reader :name, :args

    def initialize(name, args = [])
      @name, @args = name, args
      $logger.debug "call:#{to_s}"
    end

    def to_s(indent = nil)
      "#{@name}#{args_to_s(@args)}"
    end
  end

  class ClassMemberAccess
    attr_reader :instance_name, :member_name, :args

    def initialize(instance_name, member_name, args = [])
      @instance_name, @member_name, @args = instance_name, member_name, args
    end

    def to_s
      "#{@instance_name}.#{@member_name} #{@args.empty?? '' : args_to_s(@args)}"
    end
  end

  class Identifier
    attr_reader :name

    def initialize(name)
      @name = name
    end

    def to_s(indent = nil)
      @name
    end
  end

  class NewExpr
    attr_reader :class_name, :args

    def initialize(class_name, args = [])
      @class_name, @args = class_name, args
    end

    def to_s
      "#{@class_name}.new#{args_to_s(@args)}"
    end
  end

  class Instance
    attr_reader :class_define, :instance_env, :is_obj

    def initialize(class_define, member_env, is_obj = false)
      @class_define, @instance_env = class_define, member_env
      @is_obj = is_obj
    end

    def to_s
      if @is_obj
        "#{@class_define.to_s} instance"
      else
        instance_env[:_val].to_s
      end
    end

    def fetch_var(var)
      @instance_env[var]
    end

    def fetch_fun(fun)
      @class_define.fetch_member(fun)
    end

    def []=(sym, val)
      @instance_env[sym] = val
    end
  end

  class Op
    attr_accessor :op

    def initialize(op)
      @op = op
    end

    def to_s(indent = nil)
      @op
    end

    def infix
      infix_map = {'+' => 10, '-' => 10, '*' => 5, '/' => 5}
      infix_map[@op]
    end
  end

  class Constant
    attr_reader :val

    def initialize(constant_val)
      @val = constant_val
    end

    def to_s(indent = nil)
      @val
    end
  end

  class BoolConstant < Constant
  end

  class NumberConstant < Constant
  end

  class StringConstant < Constant
  end

  class DefaultValue
    def to_ast
      DefaultValue.new
    end
  end
end