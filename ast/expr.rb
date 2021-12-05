require_relative 'helper'

module Rc
  class Expr
    attr_accessor :term_list, :expr

    # Distinguish between normal expr and lambda
    def initialize(expr)
      # lambda
      if expr.class == Array
        # todo: fix code about this
        @term_list = expr
        @expr = term_list_to_operator(expr)
      else
        @term_list = [expr]
        @expr = expr
      end
    end

    def find_max_infix_index(term_list)
      # todo:maybe lowest infix can be replace with a symbol
      max_infix = 15
      max_index = term_list.size
      term_list.each_with_index do |term, index|
        next unless term.is_a? Op
        if term.infix < max_infix
          max_infix = term.infix
          max_index = index
        end
      end
      max_index
    end

    def make_binary(term_list, index)
      op = term_list[index]
      lhs = term_list[index - 1]
      rhs = term_list[index + 1]
      Binary.new(op, lhs, rhs)
    end

    def replace_operator(term_list, index)
      left = term_list.slice(0, index - 1)
      binary = [make_binary(term_list, index)]
      rights = term_list.slice(index + 2, term_list.size)
      left + binary + rights
    end

    def term_list_to_operator(term_list)
      while term_list.size != 1
        max_index = find_max_infix_index(term_list)
        term_list = replace_operator(term_list, max_index)
      end
      term_list[0]
    end

    def to_s
      @expr.to_s
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
      "(#{lhs} #{op} #{rhs})"
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