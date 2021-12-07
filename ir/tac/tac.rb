require_relative '../../ast/visitor'

module Rc
  module TAC
    def to_tac(fun)
      TacTranslator.new.generate(fun).tac_list
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
        @num.to_s
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

    class Jump
    end

    class CondJump < Jump
      attr_accessor :cond, :true_addr, :false_addr

      def initialize(cond, true_addr, false_addr)
        @cond, @true_addr, @false_addr = cond, true_addr, false_addr
      end

      def to_s
        "Cond Jump: #{@cond}? #{true_addr} #{false_addr}"
      end

      def deconstruct
        [@cond, @true_addr, @false_addr]
      end
    end

    class DirectJump < Jump
      attr_accessor :target

      def initialize(target)
        @target = target
      end

      def to_s
        "Direct Jump to #{target}"
      end

      def deconstruct
        [@target]
      end
    end

    class Label
      attr_accessor :name

      def initialize(name)
        @name = name
      end

      def to_s
        @name
      end
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

    class TacTranslator
      include Visitor
      attr_reader :tac_list

      def initialize
        @tmp_count = 0
        @tac_list = []
        @label_count = 0
      end

      def generate(fun)
        @tac_list.push Label.new(fun.name)
        visit(fun.stmts)
        @tac_list.push DirectJump.new(Label.new("TempReturnLabel"))
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
        after_if = Label.new("after_if_default")
        jump_after_if = DirectJump.new(after_if)
        node.stmt_list.each do |cond, stmts|
          cond_tac = visit(cond)
          true_label = generate_label
          cond_jump = CondJump.new(cond_tac, true_label, nil)
          @tac_list.push cond_jump
          @tac_list.push true_label
          visit(stmts)
          @tac_list.push jump_after_if
          false_label = generate_label
          cond_jump.false_addr = false_label
          @tac_list.push false_label
        end
        visit(node.else_stmts)
        @tac_list.push jump_after_if
        new_label = generate_label
        after_if.name = new_label.name
        @tac_list.push after_if
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