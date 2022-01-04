require './ir/vm/generator'

module GenTest
  class Add
  end

  class Label < TypeStruct.new(:name)
  end

  class SetLocal < TypeStruct.new(:offset => :int)
  end
end
describe 'generator' do
  before do
    @classes = get_classes(GenTest)
  end
  context 'get_classes' do
    it 'succeed' do
      [GenTest::Add, GenTest::Label, GenTest::SetLocal].each do |klass|
        expect(@classes.include? klass)
      end
    end
  end

  context 'enum inst type' do
    it 'succeed' do
      s = <<SRC
enum class InstType {
Add,
Label,
SetLocal,
}
SRC
      expect(gen_enum_inst_type(@classes)).to eq s
    end
  end

  context 'class define' do
    before do
      @add = <<SRC
struct Add : VMInst
{
  Add()
  {
    type = InstType::Add
  }
}
SRC
      @label = <<SRC
struct Label : VMInst
{
  Label()
  {
    type = InstType::Label
  }
}
SRC
      @set_local = <<SRC
struct SetLocal : VMInst
{
  SetLocal()
  {
    type = InstType::SetLocal
  }
}
SRC
    end
    context 'single class' do
      it 'empty class' do
        expect(gen_class_define(GenTest::Add)).to eq @add
      end

      it 'normal type struct class' do
        expect(gen_class_define(GenTest::Label)).to eq @label
      end

      it 'spec type struct class' do
        expect(gen_class_define(GenTest::SetLocal)).to eq @set_local
      end
    end
  end
end