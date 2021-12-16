require_relative '../../lib/helper'

module Rc
  module TAC
    module Visitor
      def visit(inst)
        method("on_#{Helper::under_score_class_name(inst)}")[inst]
      end

      def on_array(inst) = inst.map {|n| visit(n)}

      def on_op(inst) = inst

      def on_number(inst) = inst

      def on_name(inst) = inst

      def on_temp_name(inst) = inst

      def on_empty_op(inst) = inst

      def on_empty_value(inst) = inst

      def on_move(inst) = inst

      def on_tac_root(inst)
        inst.fun_list.map { |f| visit(f) }
      end

      def on_control_flow_graph(cfg)
        cfg.to_tac_list.map { |x| visit(x) }
      end

      def on_quad(inst)
        visit(inst.op)
        visit(inst.lhs)
        visit(inst.rhs)
        visit(inst.result)
        inst
      end
    end
  end
end
