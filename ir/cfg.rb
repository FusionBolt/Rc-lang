require_relative './tac/visitor'

module Rc
  module CFG
    class BasicBlock
      attr_reader :inst_list

      def initialize
        @inst_list = []
      end

      private def method_missing(symbol, *args)
        @inst_list.send(symbol, *args)
      end
    end

    class CFGTranslator
      include Tac::Visitor
    end
  end
end
