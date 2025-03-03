import {UserConfigFn} from 'vite';
import {overrideVaadinConfig} from './vite.generated';

const customConfig: UserConfigFn = () => ({
    base: "/BesserTanken/",
});

export default overrideVaadinConfig(customConfig);
