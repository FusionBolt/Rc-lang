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

      def blocks
        @blocks.values
      end

      def make_connect
        # todo:basic block to a linked list?
        blocks.map do |block|
          case block
          in BasicBlock[label, jump]
            case jump
            in TAC::DirectJump[target]
              block.all_next = [@blocks[target]]
              [Connection.new(label, target)]
            in TAC::CondJump[_, true_addr, false_addr]
              block.all_next = [@blocks[true_addr], @blocks[false_addr]]
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
      attr_accessor :all_next, :inst_list

      def initialize(begin_label)
        @inst_list = [begin_label]
      end

      def label
        @inst_list.first
      end

      def jump
        @inst_list.last
      end

      # todo:add a helper for direct get all instance var
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


    class Road
      attr_reader :list

      def initialize
        @list = []
      end

      def append(node)
        @list.push node
      end

      def to_s
        @list.map(&:to_s).join("\n")
      end
    end

    class Tag
      attr_reader :tag_value

      def initialize
        @tag_value = {}
      end

      def mark(key)
        @tag_value[key] = true
      end

      def has_marked(key)
        @tag_value.has_key? key
      end
    end

    def blocks_to_tac_list(blocks)
      blocks.reduce([]) { |block| block.inst_list }
    end

    # 1. after cond_jump is false label -> not process
    # 2. after cond_jump is true label -> 1. switch true and false; 2. cond = not cond
    # 3. generate a new label lf' and insert
    # cond_jump(cond, a, b, lt, lf')
    # label lf'
    # jump lf
    # todo: 3 need test
    def reorder_branches(cfg)
      blocks = search_all_branches(cfg)
      # lower and raise
      tac_list = blocks_to_tac_list(blocks)
      tac_list.each_with_index do |tac, index|
        if tac.is_a? TAC::CondJump
          next_tac = tac_list[index + 1]
          if next_tac == tac.false_addr
            # is ok
          elsif next_tac == tac.true_addr
            next_false_tac = tac_list[index + 2]
            tac_list[index + 1], tac_list[index + 2] = next_false_tac, next_tac
          else
            old_false_branch = tac.false_addr
            new_false_branch = TAC::Label.new("#{tac.false_addr.name}f'")
            tac.false_addr = new_false_branch
            tac_list.insert(index + 1, new_false_branch)
            TAC::DirectJump.new(old_false_branch)
          end
        end
      end
      to_cfg(tac_list)
    end

    def search_all_branches(cfg)
      blocks = cfg.blocks
      tag = Tag.new
      q = blocks
      roads = []
      # dfs that traverse all nodes
      until q.empty?
        roads.push search_single_road(q, tag)
      end
      roads.reduce([]) do |sum, road|
        sum + road.list
      end
    end

    def search_single_road(q, tag)
        t = Road.new
        b = q.shift
        until tag.has_marked(b)
          tag.mark(b)
          t.append(b)
          # find last(false branch)
          first_next_b = b.all_next.reverse.find { |next_b| not tag.has_marked(next_b) }
          if first_next_b.nil?
            break
          else
            b = first_next_b
          end
        end
        t
    end

    module_function :to_cfg, :valid_do, :search_all_branches, :search_single_road, :reorder_branches, :blocks_to_tac_list
  end
end