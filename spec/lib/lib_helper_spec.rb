require 'rspec'
require 'set'
require './lib/helper'

describe 'LibHelper' do
  before do
    # Do nothing
  end

  after do
    # Do nothing
  end

  context 'reverse_kv' do
    it 'succeeds' do
      hash = {"main" => Set["f1"], "f1" => Set["main", "f2"], "f2" => Set["main", "f1"], "f3" => Set["f2"]}
      reverse = Rc::Helper.reverse_kv(hash)
      expect(reverse["f1"]).to eq Set["main", "f2"]
    end
  end

end