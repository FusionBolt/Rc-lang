require 'rspec'
require 'set'
require_relative '../../analysis/call_graph'
require_relative '../../analysis/global_env'
require_relative '../parser_helper'

describe Rc::Analysis::CallGraph do
  before do
    # Do nothing
  end

  after do
    # Do nothing
  end

  context 'reverse_kv' do
    it 'succeeds' do
      ast = parse_demo('call_graph')
      env = Rc::Analysis::GlobalEnvVisitor.new.analysis(ast)
      call_graph = Rc::Analysis::CallGraph.new(env).analysis(env['main'])
      expect_call_map =  {"main" => Set["f1", "f2"], "f1" => Set["f2"], "f2" => Set["f1", "f3"], "f3" => Set[]}
      expect(call_graph.call_map).to eq expect_call_map
      expect(call_graph.find_usage('f1')).to eq %w[main f2]
    end
  end

end