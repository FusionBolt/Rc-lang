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
        puts br
        l = br.name
        expect(res_map.has_key? l).to eq true
        expect(br.next_labels).to eq res_map[l]
      end
    end

    it 'search_all_branches' do

    end
    it 'reorder success' do

    end
  end

end