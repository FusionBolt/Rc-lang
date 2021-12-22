require './ir/tac/visitor'
require_relative './inst'

module Rc
  module RCVM
    module MemoryLocation
      ESP = 0
      EAX = 1
      RBP = 2
    end

    def to_vm_inst(fun)
      RCVMInstTranslator.new.visit(fun)
    end

    class RCVMInstTranslator
      include TAC::Visitor

      def on_function(fun)
        fun.tac_list.map { |t| visit(t) }.flatten
      end

      def on_label(inst)
        Label.new(inst.name)
      end

      def on_quad(inst)
        v1 = Push.new(visit(inst.lhs))
        v2 = if inst.rhs.is_a? TAC::Empty
          []
        else
          Push.new(visit(inst.rhs))
        end
        op = visit(inst.op)
        res = Pop.new(visit(inst.result))
        [v1, v2, op, res]
      end

      def on_cond_jump(inst)
        # todo:fix this
        CondJump.new(inst.cond, inst.true_addr)
      end

      def on_direct_jump(inst)
        DirectJump.new(inst.target)
      end

      # todo:process this
      def on_move(inst)
        inst
      end

      def on_return(inst)
        # pop value to eax
        Pop.new(MemoryLocation::EAX)
        Return.new
      end

      def on_op(inst)
        case inst.op
        in "+"
          Add.new
        in "-"
          Sub.new
        in "*"
          Mul.new
        in "/"
          Div.new
        else
          raise "not supported op #{inst.op.class}:#{inst.op}"
        end
      end

      def on_empty_op(inst)
        []
      end

      def on_empty_value(inst)
        []
      end
    end

    module_function :to_vm_inst
  end
end
