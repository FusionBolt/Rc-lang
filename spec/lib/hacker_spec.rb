require 'rspec'
require './lib/hack'

describe 'Hack' do
  context 'hash' do
    it 'update_values' do
      a = {:a => 1, :b => 2}
      a.update_values do |key, value|
        value * 2
      end
      expect(a).to eq({:a => 2, :b => 4})
    end
  end
end
