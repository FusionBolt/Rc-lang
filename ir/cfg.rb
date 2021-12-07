require_relative './tac/visitor'
require 'ruby-graphviz'

module Rc
  module CFG
    class Connection
      attr_reader :start, :target
      def initialize(start, target)
        @start, @target = start, target
      end

      def to_s
        "#{@start} -> #{@target}"
      end
    end

    class ControlFlowGraph
      def initialize(blocks)
        @blocks = (blocks.map{|block| [block.label, block] }).to_h
        @connections = make_connect
      end

      def make_connect
        @blocks.values.map do |block|
          case block
          in BasicBlock[label, jump]
            case jump
            in TAC::DirectJump[target]
              [Connection.new(label, target)]
            in TAC::CondJump[_, true_addr, false_addr]
              [Connection.new(label, true_addr), Connection.new(label, false_addr)]
            else
              raise "Invalid Jump: #{jump}"
            end
          else
            raise "Invalid block: #{block}"
          end
        end.flatten
      end

      def to_s
        @connections.map(&:to_s).join("\n")
      end

      def to_dot(path)
        GraphViz.new(:G, :type => :digraph) do |g|
          @connections.each {|con| g.add_edge(con.start.to_s, con.target.to_s)}
          g.output(:png => path)
        end
      end
    end

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

      def deconstruct
        [label, jump]
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
      unless tac_list[0].is_a? TAC::Label
        # todo:error process
        raise 'Fun tac_list should begin with label, please check code which generate by tac'
      end
      blocks = []
      tmp_label_count = 0
      tac_list.each_with_index do |cur_tac, index|
        if cur_tac.is_a? TAC::Label
          # prev is not a jump, maybe need push a jump to this label
          # but when first, not need process
          valid_do(tac_list, index - 1) do |prev_tac|
            unless prev_tac.is_a? TAC::Jump
              blocks.last.push TAC::DirectJump.new(cur_tac)
            end
          end
          blocks.push BasicBlock.new(cur_tac)
        elsif cur_tac.is_a? TAC::Jump
          # next is not a label, need create a block and push a label to next block
          blocks.last.push cur_tac
          valid_do(tac_list, index + 1) do |next_tac|
            unless next_tac.is_a? TAC::Label
              blocks.push BasicBlock.new("TmpLabel#{tmp_label_count}")
              # push a label
            end
          end
        else
          blocks.last.push cur_tac
        end
      end
      # todo:add check valid
      ControlFlowGraph.new(blocks)
    end

    module_function :to_cfg, :valid_do
  end
end