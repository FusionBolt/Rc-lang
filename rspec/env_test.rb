require 'minitest/autorun'
require_relative '../env'

class EnvTest < Minitest::Unit::TestCase
  include Rc
  def setup
    @env = Env.new
    # Do nothing
  end

  def teardown
    # Do nothing
  end

  def test_define
    @env.define_symbol('a', 1)
    assert @env.has_key?('a')
    assert_equal @env['a'], 1
  end

  def test_symbol_re_define_err
    @env.define_symbol('a', 1)
    assert_raises SymbolReDefineError do
      @env.define_symbol('a', 10)
    end
  end

  def test_no_symbol_define_err
    assert_raises SymbolNotFoundError do
      @env.find_symbol('a')
    end
  end

  def test_symbol_not_found_err
    @env['a'] = 1
    assert_raises SymbolNotFoundError do
      @env.update_symbol('b', 1)
    end
    @env.update_symbol('a', 2)
    assert_equal @env['a'], 2
  end

  def test_nest_env
    @env.define_symbol('a', 1)
    @env.send(:start_subroutine)
    assert_equal @env.env, {}
    assert_equal @env.outer.find_symbol('a'), 1
  end

  def test_subroutine
    @env.define_symbol('a', 1)
    @env.define_symbol('b', 1)
    @env.sub_scope({}) do
      @env.define_symbol('a', 2)
      assert_equal @env.find_symbol('a'), 2
      assert_equal @env.find_symbol('b'), 1
      @env.sub_scope({}) do
        assert_equal @env.find_symbol('a'), 2
        assert_equal @env.find_symbol('b'), 1
        @env.define_symbol('c', 9)
      end
      assert_equal @env.find_symbol('a'), 2
      assert_equal @env.find_symbol('b'), 1
      assert_raises SymbolNotFoundError do
        @env.find_symbol('c')
      end
    end
    assert_equal @env.outer, nil
    assert @env.env != {}
    assert_equal @env.find_symbol('a'), 1
    assert_equal @env.find_symbol('b'), 1
  end
end