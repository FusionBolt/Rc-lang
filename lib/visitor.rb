require_relative 'helper'

module Rc
  module Lib
    module Visitor
      def visit(node)
        begin
          method("on_#{Rc::Helper::under_score_class_name(node)}")[node]
        rescue NoMethodError => e
          # todo:error process
          $logger.error "Error in visitor\nnode:#{node}\nerror info:#{e}"
          exit
        end
      end
    end
  end
end