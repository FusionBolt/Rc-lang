require 'rspec'
require './lib/env'

describe 'Env' do
  before do
    @env = Rc::Env.new
    @env['a'] = 1
    # Do nothing
  end

  after do
    # Do nothing
  end

  context 'define symbol' do
    it 'succeeds' do
      expect(@env.has_key?('a'))
      expect(@env['a']).to eq 1
    end

    it 'symbol re define error' do
      expect {
        @env.define_symbol('a', 10)
      }.to raise_error(Rc::SymbolReDefineError)
    end
  end

  context 'find symbol' do
    it 'succeeds' do
      expect(@env.find_symbol('a')).to eq 1
    end

    it 'not found' do
      expect {
        @env.find_symbol('b')
      }.to raise_error(Rc::SymbolNotFoundError)
    end

    it 'find symbol after define' do
      @env.define_symbol('sym', 'str')
      expect(@env.find_symbol('sym')).to eq 'str'
    end
  end

  context 'update symbol' do
    it 'succeeds' do
      @env.update_symbol('a', 2)
      expect(@env['a']).to eq 2
    end

    it 'no symbol' do
      expect {
        @env.update_symbol('b', 1)
      }.to raise_error Rc::SymbolNotFoundError
    end
  end

  context 'nest env' do
    it 'start_subroutine' do
      @env.send(:start_subroutine)
      expect(@env.env).to be {}
      expect(@env.outer.find_symbol('a')).to eq 1
    end

    it 'subroutine' do
      @env['b'] = 1
      old_env = @env.env
      @env.sub_scope({}) do
        expect(@env.outer.env).to be old_env
        @env.define_symbol('a', 2)
        expect(@env.find_symbol('a')).to eq 2
        expect(@env.find_symbol('b')).to eq 1
        @env.sub_scope({}) do
          expect(@env.find_symbol('a')).to eq 2
          expect(@env.find_symbol('b')).to eq 1
          @env['c'] = 9
        end
        expect(@env.find_symbol('a')).to eq 2
        expect(@env.find_symbol('b')).to eq 1
        expect {
          @env.find_symbol('c')
        }.to raise_error Rc::SymbolNotFoundError
      end
      expect(@env.outer).to eq nil
      expect(@env.env).to eq ({ 'a' => 1, 'b' => 1 })
      expect(@env.find_symbol('a')).to eq 1
      expect(@env.find_symbol('b')).to eq 1
    end
  end
end