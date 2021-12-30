require_relative 'helper'

module Rc
  module Lib
    module Visitor
      def visit(node)
          method("on_#{Rc::Helper::under_score_class_name(node)}")[node]
      end
    end
  end
end