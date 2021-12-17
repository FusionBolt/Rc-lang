require 'rspec'
require 'set'
require_relative '../../ir/cfg'
require_relative 'ir_helper'

describe 'CFG' do
  before do
    @src = <<CFG_TEST
def main
    if true
        q = 1
    else
        q = 2
        if false
            s = 1
        elsif true
            s = 2
        else
            s = 3
        end
    end
    n = 1
end
CFG_TEST
    # Do nothing
  end

  after do
    # Do nothing
  end

  context 'to_cfg' do
    it 'succeeds' do
      tac = get_tac(@src)
      tac.process { |f| Rc::CFG.to_cfg(f.tac_list) }
    end
  end

  context 'reorder_branches' do
    before do
      tac = get_tac(@src)
      tac.process { |f| Rc::CFG.to_cfg(f.tac_list) }
      @main = tac.fun_list[0]
    end

    it 'search_single_road' do
      q = @main.blocks
      tag = Rc::CFG::Tag.new
      road = Rc::CFG.search_single_road(q, tag)
      error_block = road.blocks.filter { |block| block.name == 'L0' }
      expect(error_block).to eq []
      # todo:check order and more road
    end

    it 'search_all_branches' do
      all_branches = Rc::CFG.search_all_branches(@main)
      expect(all_branches.size).to eq 9
      res_map = {
        'main' => %w[L0 L1],
        'L1' => %w[L2 L3],
        'L3' => %w[L4 L5],
        'L5' => %w[L6],
        'L6' => %w[L7],
        'L7' => [nil],
        'L0' => %w[L7],
        'L2' => %w[L6],
        'L4' => %w[L6],
      }
      all_branches.each do |br|
        l = br.name
        expect(res_map.has_key? l).to eq true
        expect(br.next_labels).to eq res_map[l]
      end
    end

    context 'reorder' do
      before do
        @t = Rc::TAC::Label.new('l1')
        @f = Rc::TAC::Label.new('l2')
        @cond_jump = Rc::TAC::CondJump.new('true', @t, @f)
        @o = Rc::TAC::Label.new('other')
      end

      it 'after_cond_jump_is_false_label' do
        order = [@cond_jump, @f, @t]
        before_order = order.clone
        result = Rc::CFG.reorder_branches_impl(order)
        expect(result).to eq before_order
      end

      it 'after_cond_jump_is_true_label' do
        order = [@cond_jump, @t, @f]
        result = Rc::CFG.reorder_branches_impl(order)
        expect(result).to eq [@cond_jump, @f, @t]
      end

      it 'after_cond_jump_is_not_relative_label' do
        # [@cond_jump, new_l, new_direct_jump, @o, @t, @f]
        order = [@cond_jump, @o, @t, @f]
        old_label = order[1..].clone
        result = Rc::CFG.reorder_branches_impl(order)
        new_false_name = "#{@f.name}f'"
        new_l = Rc::TAC::Label.new(new_false_name)
        new_direct_jump = Rc::TAC::DirectJump.new(@f)
        expect(result.size).to eq 6
        expect(result[0].false_addr).to eq result[1]
        expect(result[1..5]).to eql [new_l, new_direct_jump] + old_label
      end
    end
  end
end