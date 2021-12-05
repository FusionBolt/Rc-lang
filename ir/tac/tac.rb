require_relative '../../ast/visitor'

module Rc
  module Tac
    def to_tac(ast)
      TacGenerator.new.generate(ast.stmts).tac_list
    end

    class TacInst
      attr_accessor :op, :result, :lhs, :rhs

      def initialize(op, result, lhs, rhs)
        @op, @result, @lhs, @rhs = op, result, lhs, rhs
      end

      def to_s
        "#{@result} = #{@lhs} #{@op} #{@rhs}"
      end
    end

    class Name
      attr_accessor :name

      def initialize(name)
        @name = name
      end

      def to_s
        @name.gsub(/:/, '')
      end
    end

    class TempName < Name
    end

    class Number
      attr_accessor :num

      def initialize(num)
        @num = num
      end

      def to_s
        @num
      end
    end

    class Loop
    end

    class Bn
    end

    class Call
    end

    class Mem
    end

    class CondJump
    end

    class Label
    end

    class Empty
      def to_s
        ""
      end
    end

    class EmptyOp < Empty
    end

    class EmptyValue < Empty
    end

    class TacGenerator
      include Visitor
      attr_reader :tac_list

      def initialize
        @tmp_count = 0
        @tac_list = []
      end

      def generate(node)
        visit(node)
        self
      end

      def get_tmp
        TempName.new("#{@tmp_count}").tap{ @tmp_count += 1}
      end

      # def on_stmts(node)
      #   node.stmts.reduce([]) { |sum, n| sum + [visit(n)] }
      # end

      def on_assign(node)
        name = visit(node.var_obj)
        expr = visit(node.expr)
        if expr.is_a? TacInst
          @tac_list[-1].result = name
        else
          inst = TacInst.new(EmptyOp.new, name, expr, EmptyValue.new)
          @tac_list.push inst
        end
        # translate last tac
      end

      def get_result(ret_val)
        if ret_val.is_a? TacInst
          ret_val.result
        else
          ret_val
        end
      end

      def on_binary(node)
        first_tac = visit(node.lhs)
        second_tac = visit(node.rhs)
        inst = TacInst.new(node.op, get_tmp, get_result(first_tac), get_result(second_tac))
        @tac_list.push inst
        inst
      end

      def on_lambda(node) end

      def on_fun_call(node) end

      def on_class_member_access(access) end

      def on_identifier(node)
        Name.new(node.name)
      end

      def on_instance(node) end

      def on_new_expr(node) end

      def on_constant(node) end

      def on_bool_constant(node)
        Number.new(node.val == 1)
      end

      def on_number_constant(node)
        Number.new(node.val)
      end

      def on_string_constant(node) end
    end

    module_function :to_tac
  end
end