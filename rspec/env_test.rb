require 'minitest/autorun'
require_relative '../env'

class EnvTest < Minitest::Unit::TestCase
  def setup
    @env = Env.new
    # Do nothing
  end

  def teardown
    # Do nothing
  end

  def test_redefine_index
    @env['a'] = 1
    assert @env.has_key?('a')
    assert_equal @env['a'], 1
  end

  def test_symbol_re_define_err
    @env.define_symbol('a', 1)
    assert_raises RuntimeError do
      @env.define_symbol('a', 10)
    end
  end

  def test_no_symbol_define_err
    assert_raises RuntimeError do
      @env.find_symbol('a')
    end
  end

  def test_symbol_not_found_err
    @env['a'] = 1
    assert_raises RuntimeError do
      @env.update_symbol('b', 1)
    end
    @env.update_symbol('a', 2)
    assert_equal @env['a'], 2
  end
end