require_relative '../../ast/visitor'

module Rc
  module Tac
    def to_tac(ast)
      TacTranslator.new.generate(ast.stmts).tac_list
    end

    class Quad
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
      attr_accessor :cond, :true_addr, :false_addr

      def initialize(cond, equality, false_addr)
        @cond, @equality, @false_addr = cond, equality, false_addr
      end
    end

    class Label
      attr_reader :name

      def initialize(name)
        @name = name
      end
    end

    class Empty
      def to_s
        "empty"
      end
    end

    class EmptyOp < Empty
    end

    class EmptyValue < Empty
    end

    class TacTranslator
      include Visitor
      attr_reader :tac_list

      def initialize
        @tmp_count = 0
        @tac_list = []
        @label_count = 0
      end

      def generate(node)
        visit(node)
        self
      end

      def get_tmp_name
        TempName.new("#{@tmp_count}").tap{ @tmp_count += 1 }
      end

      def generate_label
        Label.new("L#{@label_count}").tap{ @label_count += 1 }
      end

      # def on_stmts(node)
      #   node.stmts.reduce([]) { |sum, n| sum + [visit(n)] }
      # end

      def on_assign(node)
        name = visit(node.var_obj)
        expr = visit(node.expr)
        if expr.is_a? Quad
          @tac_list[-1].result = name
        else
          inst = Quad.new(EmptyOp.new, name, expr, EmptyValue.new)
          @tac_list.push inst
        end
        # translate last tac
      end

      def get_result(ret_val)
        if ret_val.is_a? Quad
          ret_val.result
        else
          ret_val
        end
      end

      def on_binary(node)
        first_tac = visit(node.lhs)
        second_tac = visit(node.rhs)
        inst = Quad.new(node.op, get_tmp_name, get_result(first_tac), get_result(second_tac))
        @tac_list.push inst
        inst
      end

      def on_if(node)
        puts node
        node.stmt_list.each do |cond, stmts|
          cond_tac = visit(cond)
          true_label = generate_label
          @tac_list.push true_label
          jump = CondJump.new(cond_tac, true_label, nil)
          @tac_list.push jump
          visit(stmts)
          false_label = generate_label
          jump.false_addr = false_label
          @tac_list.push false_label
        end
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