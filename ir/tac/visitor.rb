require './lib/helper'
require './lib/visitor'

module Rc
  module TAC
    module QuadVisitor
      def on_quad(inst)
        visit(inst.op)
        visit(inst.lhs)
        visit(inst.rhs)
        visit(inst.result)
        inst
      end

      def on_assign(inst)
      end

      def on_call(inst)
      end
    end

    module OperandVisitor
      def on_number(inst) = inst

      def on_name(inst) = inst

      def on_temp_name(inst) = inst
    end

    module Visitor
      include Rc::Lib::Visitor
      include QuadVisitor
      include OperandVisitor

      def on_array(inst) = inst.map {|n| visit(n)}

      def on_op(inst) = inst

      def on_move(inst) = inst

      def on_tac_root(inst)
        inst.fun_list.map { |f| visit(f) }
      end

      def on_control_flow_graph(cfg)
        cfg.to_tac_list.map { |x| visit(x) }
      end
    end
  end
end
