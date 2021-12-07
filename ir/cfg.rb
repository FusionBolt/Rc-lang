require_relative './tac/visitor'

module Rc
  module CFG
    class BasicBlock
      def initialize(begin_label)
        @inst_list = [begin_label]
      end

      def label
        @inst_list.first
      end

      def jump
        @inst_list.last
      end

      def to_s
        "BasicBlock #{label} #{jump}"
      end

      def push(inst)
        @inst_list.push inst
      end
    end

    # todo:array hack
    def valid_do(array, index, &block)
      if index >= 0 && index < array.size
        block.call array[index]
      end
    end

    def to_cfg(tac_list)
      # todo:insert label or jump impl and test
      unless tac_list[0].is_a? Tac::Label
        # todo:error process
        raise 'Fun tac_list should begin with label, please check code which generate by tac'
      end
      blocks = []
      tmp_label_count = 0
      tac_list.each_with_index do |cur_tac, index|
        if cur_tac.is_a? Tac::Label
          # prev is not a jump, maybe need push a jump to this label
          # but when first, not need process
          valid_do(tac_list, index - 1) do |prev_tac|
            unless prev_tac.is_a? Tac::Jump
              blocks.last.push Tac::DirectJump.new(cur_tac)
            end
          end
          blocks.push BasicBlock.new(cur_tac)
        elsif cur_tac.is_a? Tac::Jump
          # next is not a label, need create a block and push a label to next block
          blocks.last.push cur_tac
          valid_do(tac_list, index + 1) do |next_tac|
            unless next_tac.is_a? Tac::Label
              blocks.push BasicBlock.new("TmpLabel#{tmp_label_count}")
              # push a label
            end
          end
        else
          blocks.last.push cur_tac
        end
      end
      blocks
    end

    module_function :to_cfg, :valid_do
  end
end