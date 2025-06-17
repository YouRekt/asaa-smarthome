import { ThemeModeToggle } from "@/components/ui/theme-mode-toggle";
import { Home } from "lucide-react";

const Sidebar = ({ children }: React.PropsWithChildren) => {
	return (
		<div className="flex w-64 flex-col">
			<div className="flex flex-col flex-grow pt-5 border-r overflow-y-auto bg-card">
				<div className="flex items-center flex-shrink-0 px-4 text-primary">
					<Home className="size-8" />
					<span className="ml-2 text-xl font-semibold">
						Smart Home
					</span>
					<span className="ml-auto">
						<ThemeModeToggle />
					</span>
				</div>
				{children}
			</div>
		</div>
	);
};
export default Sidebar;
