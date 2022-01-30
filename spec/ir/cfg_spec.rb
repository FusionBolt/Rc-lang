require 'rspec'
require 'set'
require './ir/cfg'
require_relative 'ir_helper'

describe 'CFG' do
  before do
    pending("waiting refactor tac, because of method in kernels")
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
        expect(res_map.has_key? l)
        expect(br.next_labels).to eq res_map[l]
      end
    end

    context 'reorder' do
      before do
        @n = Rc::TAC::Number.new(1)
        @t = Rc::TAC::Label.new('l1')
        @f = Rc::TAC::Label.new('l2')
        @cond_jump = Rc::TAC::CondJump.new(@n, @t, @f)
        @o = Rc::TAC::Label.new('other')
      end

      it 'after_cond_jump_is_false_label' do
        order = [@n, @cond_jump, @f, @t]
        before_order = order.clone
        result = Rc::CFG.reorder_branches_impl(order)
        expect(result).to eq before_order
      end

      it 'after_cond_jump_is_true_label' do
        order = [@n, @cond_jump, @t, @f]
        old_n = @n
        result = Rc::CFG.reorder_branches_impl(order)
        expect(result[0]).to be old_n
        expect(result[1].op).to eq '!'
        expect(result[1].lhs).to be result[0]
        expect(result[1].rhs).to be nil
        expect(result[2].cond).to be result[1]
        expect(result[2..-1]).to eq [@cond_jump, @f, @t]
      end

      it 'after_cond_jump_is_not_relative_label' do
        # [@n, @cond_jump, new_l, new_direct_jump, @o, @t, @f]
        order = [@n, @cond_jump, @o, @t, @f]
        old_label = order[2..].clone
        result = Rc::CFG.reorder_branches_impl(order)
        new_false_name = "#{@f.name}f'"
        new_l = Rc::TAC::Label.new(new_false_name)
        new_direct_jump = Rc::TAC::DirectJump.new(@f)
        expect(result.size).to eq 7
        expect(result[1].false_addr).to eq result[2]
        expect(result[2..-1]).to eq [new_l, new_direct_jump] + old_label
      end
    end
  end
end