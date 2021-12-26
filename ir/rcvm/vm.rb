require './ir/tac/visitor'
require './lib/env'
require_relative './inst'

module Rc
  module RCVM
    module MemoryLocation
      ESP = 0
      EAX = 1
      RBP = 2
    end

    def to_vm_inst(fun)
      list = RCVMInstTranslator.new.visit(fun)
      sym_table = analysis_sym_table(list)
      update_call_addr(list, sym_table)
    end

    def analysis_sym_table(list)
      sym_table = Env.new
      # todo: maybe a slow impl
      list.each_with_index do |inst, index|
        if inst.is_a? Label
          sym_table.define_symbol inst.name, index
        end
      end
      sym_table
    end

    def update_call_addr(list, sym_table)
      list.filter { |x| x.is_a? UnsetAddr }.map { |a| Addr.new('code', sym_table[a]) }
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
        v2 = if inst.rhs.is_a? TAC::EmptyValue
               []
             else
               Push.new(visit(inst.rhs))
             end
        op = process_op(inst.op)
        res = Pop.new(visit(inst.result))
        [v1, v2, op, res]
      end

      def on_cond_jump(inst)
        # todo:fix this
        CondJump.new(inst.cond, UnsetAddr.new(inst.true_addr.name))
      end

      def on_direct_jump(inst)
        DirectJump.new(UnsetAddr.new(inst.target.name))
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

      def process_op(op)
        case op
        in "+"
          Add.new
        in "-"
          Sub.new
        in "*"
          Mul.new
        in "/"
          Div.new
        in "assign"
          # todo:which should pop
          Pop.new(0)
        else
          raise "not supported op #{op}"
        end
      end

      def on_empty_op(inst)
        []
      end

      def on_empty_value(inst)
        []
      end

      def on_call(inst)
        # todo:set a error addr
        # find in env and push?
        inst
      end
    end

    module_function :to_vm_inst, :analysis_sym_table, :update_call_addr
  end
end
